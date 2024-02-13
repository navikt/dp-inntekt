CREATE INDEX CONCURRENTLY idx_inntekt_person_mapping
    ON inntekt_v1_person_mapping (aktørid, fnr, kontekstid, konteksttype, beregningsdato, "timestamp" DESC)
